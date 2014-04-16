require 'rubygems'
require 'neography'
require 'mysql2'
require 'json'

PROCESS_MAIN_CATEGORY = false

db_configs = JSON.parse(File.read 'graph/configs/database.json')
mysql_client = Mysql2::Client.new(
  host: db_configs['host'],
  username: db_configs['username'],
  password: db_configs['password'],
  port: db_configs['port'],
  database: db_configs['database']
)

main_category_ids = File.read('graph/data/main_categories.txt').split("\n").join(', ')
SQL_TEMPLATE = "
  SELECT {select}
  FROM page_categories AS pc1
    JOIN page_categories AS pc2
      ON pc1.pages = pc2.pages
          AND pc1.id {main_categories} IN (#{main_category_ids})
      JOIN Page AS p ON pc2.id = p.id
      JOIN Category AS c ON pc2.pages = c.id
  WHERE pc1.id = {page_id} AND pc2.id != {page_id}
"

def make_sql(select, only_main_categories, page_id, limit = nil)
  sql = SQL_TEMPLATE.dup
  sql.gsub!('{select}', select)
  sql.gsub!('{main_categories}', only_main_categories ? '' : 'NOT')
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
  puts "#{index + 1}. Processing #{PROCESS_MAIN_CATEGORY ? 'main' : 'sub' } category of [#{level1_id}, #{level1_title}]"

  links_count = mysql_client.query(
      make_sql('COUNT(*)', PROCESS_MAIN_CATEGORY, level1_id)).first['COUNT(*)'].to_i
  batch_size = 100
  processed_links_count = 0

  while processed_links_count < links_count
    puts "-- processing #{processed_links_count} of #{links_count}"
    links = mysql_client.query(
        make_sql(
          'p.id as page_id, p.name as page_title, c.id as category_id, c.name as category_title ',
          PROCESS_MAIN_CATEGORY, level1_id, "#{processed_links_count}, #{batch_size}"
        )
    )
    processed_links_count += batch_size

    links.each do |link|
      unless created_ids_cache[link['page_id'].to_s]
        neo4j_client.execute_query("
            MERGE (p:Page {id: #{link['page_id']}, title: \"#{link['page_title']}\"})")
        created_ids_cache[link['page_id'].to_s] = true
      end

      unless created_ids_cache[link['category_id'].to_s]
        neo4j_client.execute_query("
            MERGE (p:Category {id: #{link['category_id']}, title: \"#{link['category_title']}\", main: true})")
        created_ids_cache[link['category_id'].to_s] = true
      end

      key = link['page_id'].to_s + '-' + level1_id.to_s + '-' + link['category_id'].to_s
      unless created_ids_cache[key]
        neo4j_client.execute_query("
            MATCH
              (p:Page {id: #{link['page_id']}}),
              (l:Page {id: #{level1_id}}),
              (c:Category {id: #{link['category_id']}})
            MERGE p -[:BELONGS_TO]-> c <-[:L1_BELONGS_TO]- l")
        created_ids_cache[key] = true
      end
    end
  end
end


