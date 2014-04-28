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
  pages = MYSQL.query(FIND_PAGES_SQL % [escaped_keyword, relation.join(", ")]).map { |p| p['page'] }
  pages
end

EVALUATION_1_FILE = "tmp/evaluation1.csv"
EVALUATION_2_FILE = "tmp/evaluation2.csv"

def create_evaluation_file(file)
  open(file, 'w+') { |f| f << "keywords, #{RELATIONS.keys.join(', ')}\n" }
end
create_evaluation_file(EVALUATION_1_FILE)
create_evaluation_file(EVALUATION_2_FILE)
def write_evaluation_file(file, results)
  open(file, 'a') { |f| f << results.join(', ').gsub(/\"/, '""') + "\n" }
end

def evaluate_1(pages)
  0
end

def evaluate_2(pages)
  0
end

MAXIMUM_PAGES = 400
ALL_KEYWORDS_SQL = %(SELECT DISTINCT keyword, date FROM pages)
keywords = MYSQL.query(ALL_KEYWORDS_SQL).map { |k| { name: k['keyword'], date: k['date'] } }
keywords.each do |keyword|
  puts "Processing #{keyword[:name]}"

  evaluation_result_1 = [keyword[:name]]
  evaluation_result_2 = [keyword[:name]]

  RELATIONS.each do |name, value|
    pages = find_pages(keyword[:name], value)
    puts " - processing #{name} pages (size: #{pages.length})"
    if pages.length > MAXIMUM_PAGES
      puts " - reach maximum size #{MAXIMUM_PAGES}, skipped"
      evaluation_result_1 << 0
      evaluation_result_2 << 0
    else
      puts " - doing evaluation 1"
      result = evaluate_1(pages)
      evaluation_result_1 << result
      puts " - doing evaluation 2"
      result = evaluate_2(pages)
      evaluation_result_2 << result
    end
  end

  write_evaluation_file(EVALUATION_1_FILE, evaluation_result_1)
  write_evaluation_file(EVALUATION_2_FILE, evaluation_result_2)
end


