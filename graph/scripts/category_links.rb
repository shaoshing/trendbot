# Encoding: utf-8

require 'rubygems'
require 'neography'
require 'mysql2'
require 'json'

CATEGORY_LEVEL = 2

db_configs = JSON.parse(File.read 'graph/configs/database.json')
mysql_client = Mysql2::Client.new(
  host: 'localhost',
  username: 'root',
  database: 'wiki-langs'
)

category_title_map_id = {}
category_titles = []
File.read("graph/data/level#{CATEGORY_LEVEL}_categories.txt").lines.each do |line|
  id, title = line.strip.split(", ", 2)
  category_titles << title
  category_title_map_id[title] = id
end

SQL_TEMPLATE = %[
  SELECT {select}
  FROM categorylinks AS cl
  JOIN categorylinks AS cl2 ON cl.cl_to = cl2.cl_to AND cl2.cl_type = "page"
  JOIN page AS p ON cl2.cl_from = p.page_id
  WHERE cl.cl_to IN ("#{category_titles.join('", "')}") AND
    cl.cl_from = {page_id} AND
    cl.cl_type = "page"
]

def make_sql(select, page_id, limit = nil)
  sql = SQL_TEMPLATE.dup
  sql.gsub!('{select}', select)
  sql.gsub!('{page_id}', page_id)
  sql += " LIMIT #{limit}" if limit
  sql
end

neo4j_client = Neography::Rest.new

created_ids_cache = {}

pages = File.read('graph/data/level1_pages.txt').lines
start_page_index = 0
(start_page_index...pages.size).each do |index|
  line = pages[index]
  level1_id, level1_title = line.strip.split(', ', 2)
  puts "#{index + 1}. Processing level #{CATEGORY_LEVEL} category of page [#{level1_id}, #{level1_title}]"

  links_count = mysql_client.query(
      make_sql('COUNT(*)', level1_id)).first['COUNT(*)'].to_i
  batch_size = 100
  processed_links_count = 0

  while processed_links_count < links_count
    puts "-- processing #{processed_links_count} of #{links_count}"
    links = mysql_client.query(
        make_sql(
          'p.page_id AS page_id, p.page_title AS page_title, cl2.cl_to AS category_title',
          level1_id, "#{processed_links_count}, #{batch_size}"
        )
    )
    processed_links_count += batch_size

    links.each do |link|
      link['page_title'] = link['page_title'].force_encoding('UTF-8')
      link['category_title'] = link['category_title'].force_encoding('UTF-8')

      unless created_ids_cache[link['page_id'].to_s]
        neo4j_client.execute_query("
            MERGE (p:Page {
              id: #{link['page_id']},
              title: \"#{mysql_client.escape(link['page_title'])}\"
            })")
        created_ids_cache[link['page_id'].to_s] = true
      end

      unless created_ids_cache[link['category_title'].to_s]
        neo4j_client.execute_query("
            MERGE (p:Category {
              id: #{category_title_map_id[link['category_title']]},
              title: \"#{mysql_client.escape(link['category_title'])}\",
              level: #{CATEGORY_LEVEL}
            })")
        created_ids_cache[link['category_title'].to_s] = true
      end

      key = link['page_id'].to_s + '-' + level1_id.to_s + '-' + link['category_title'].to_s
      unless created_ids_cache[key]
        neo4j_client.execute_query("
            MATCH
              (p:Page {id: #{link['page_id']}}),
              (l:Page {id: #{level1_id}}),
              (c:Category {id: #{category_title_map_id[link['category_title']]}})
            MERGE p -[:BELONGS_TO]-> c
            MERGE c <-[:L1_BELONGS_TO]- l")
        created_ids_cache[key] = true
      end
    end
  end
end
