# Encoding: utf-8

require 'rubygems'
require 'neography'
require 'mysql2'
require 'json'

MYSQL = Mysql2::Client.new(
  host: 'localhost',
  username: 'root',
  database: 'graph'
)

RELATIONS = {
  incoming: [1],
  outgoing: [2],
  incoming_and_outgoing: [1, 2],
  l2_category: [3],
  l3_category: [4]
}

FIND_PAGES_SQL = %(SELECT page FROM pages WHERE keyword = "%s" AND relation IN (%s);)
def find_pages(keyword, relation)
  escaped_keyword = MYSQL.escape(keyword)
  pages = MYSQL.query(FIND_PAGES_SQL % [escaped_keyword, relation.join(', ')])
  pages.map { |p| p['page'].upcase }
end

EVALUATION_1_FILE = 'tmp/evaluation1-popularity.csv'
EVALUATION_2_FILE = 'tmp/evaluation2-page-view.csv'
open(EVALUATION_1_FILE, 'w+') { |f| f << "keywords, incoming, incoming popular %, outgoing, outgoing popular %, in and out, in and out popular %, l2 categories, l2 categories popular %, l3 categories, l3 categories popular %\n" }
open(EVALUATION_2_FILE, 'w+') { |f| f << "keywords, incoming, incoming pv %, outgoing, outgoing pv %, in and out, in and out pv %, l2 categories, l2 categories pv %, l3 categories, l3 categories pv %\n" }

def write_evaluation_file(file, results)
  open(file, 'a') { |f| f << results.join(', ').gsub(/\"/, '""') + "\n" }
end

# {
#   "title": {  }
# }

DAYS_BACK = 2
DATE_FORMAT_SQL = '%Y%m%d'
PAGE_VIEWS_SQL = %(
    SELECT pagetitle, pageviewcount, DATE_FORMAT(datetime, "%s") AS date
    FROM pagecount
    WHERE datetime >= "%s" AND datetime < "%s" AND pagetitle IN ("%s")
    )

# returns
# {
#   "title": {20120201: 200, ...}
# }
CACHE = {}
def get_page_views(pages, date)
  cache_key = (pages.hash+date.hash).hash

  return CACHE[cache_key] if CACHE[cache_key]

  # initialize default page views
  default_views = {}
  ((-DAYS_BACK)..DAYS_BACK).each do |d|
    default_views[ (date+d).strftime('%Y%m%d').to_i ] = 0
  end

  views = {}
  pages.each do |p|
    views[p] = default_views.dup
  end

  # update actual page views from mysql
  beginning_of_date = date.prev_day(DAYS_BACK)
  end_of_date = date.next_day(DAYS_BACK+1)
  escaped_pages = pages.map { |p| MYSQL.escape(p) }
  sql = PAGE_VIEWS_SQL % [DATE_FORMAT_SQL, beginning_of_date, end_of_date, escaped_pages.join('", "')]

  page_views = MYSQL.query(sql)
  page_views.each do |pv|
    date = pv['date'].to_i
    page_title = pv['pagetitle'].upcase

    # ANGEL_DI_MARIA
    # ÁNGEL_DI_MARíA
    next unless views[page_title]

    views[page_title][date] += pv['pageviewcount']
  end

  CACHE[cache_key] = views
  views
end

# returns % of popular pages
PAGE_VIEW_DIFF = 500
def evaluate_1(pages, date)
  popular_page_count = 0
  views = get_page_views(pages, date)
  dateValue = date.strftime('%Y%m%d').to_i
  views.each do |title, v|
    # puts "----"
    # puts date
    # puts title
    # puts v
    # puts "----"
    average = v.values.inject { |sum,x| sum + x } / v.values.length
    view_of_the_date = v[dateValue]
    popular_page_count += 1 if view_of_the_date > average + PAGE_VIEW_DIFF
  end

  (popular_page_count / pages.length.to_f).round(3)
end

TOTAL_PAGE_VIEWS = {}
# returns total pages view and the % of page views contributed
def evaluate_2(pages, date)
  if TOTAL_PAGE_VIEWS.length == 0
    MYSQL.query('SELECT * FROM total_page_views').each do |p|
      TOTAL_PAGE_VIEWS[p['date'].to_i] = p['total_page_views']
    end
  end

  views = get_page_views(pages, date)
  dateValue = date.strftime('%Y%m%d').to_i

  total_page_views = TOTAL_PAGE_VIEWS[dateValue]
  return 0, 0 unless total_page_views

  contributed_page_views = 0
  views.each do |title, v|
    contributed_page_views += v[dateValue]
  end

  return total_page_views, (contributed_page_views / total_page_views.to_f).round(4)
end

MAXIMUM_PAGES = 400
ALL_KEYWORDS_SQL = %(
    SELECT DISTINCT keyword, date
    FROM pages
    WHERE date >= 20140203 AND date <= 20140213
    ORDER BY date
    )
keywords = MYSQL.query(ALL_KEYWORDS_SQL).map { |k| { name: k['keyword'], date: k['date'] } }
keywords.each do |keyword|

  keyword_date = Date.strptime(keyword[:date].to_s, "%Y%m%d")
  puts "Processing [#{keyword[:name]}] - #{keyword_date}"

  evaluation_result_1 = [keyword[:name]]
  evaluation_result_2 = [keyword[:name]]

  RELATIONS.each do |name, value|
    pages = find_pages(keyword[:name], value)
    puts " - processing #{name} pages (size: #{pages.length})"
    if pages.length > MAXIMUM_PAGES || pages.length == 0
      puts " - reach maximum size #{MAXIMUM_PAGES}, skipped"
      evaluation_result_1 += [0, 0]
      evaluation_result_2 += [0, 0]
    else
      # puts " - doing evaluation 1"
      # popular_pages = evaluate_1(pages, keyword_date)
      # evaluation_result_1 << pages.length
      # evaluation_result_1 << popular_pages
      puts " - doing evaluation 2"
      total_page_views, contributed_page_views = evaluate_2(pages, keyword_date)
      evaluation_result_2 << total_page_views
      evaluation_result_2 << contributed_page_views
    end
  end

  write_evaluation_file(EVALUATION_1_FILE, evaluation_result_1)
  write_evaluation_file(EVALUATION_2_FILE, evaluation_result_2)
end


