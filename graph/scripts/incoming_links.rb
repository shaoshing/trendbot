require 'rubygems'
require 'neography'
require 'mysql2'
require 'json'

db_configs = JSON.parse(File.read 'graph/configs/database.json')
mysql_client = Mysql2::Client.new(
  host: db_configs['host'],
  username: db_configs['username'],
  password: db_configs['password'],
  port: db_configs['port'],
  database: db_configs['database']
)

neo4j_client = Neography::Rest.new

level1_title_map_id = {}

File.read('graph/data/level1_pages.txt').lines.each do |line|
  id, title = line.strip.split(', ', 2)
  level1_title_map_id[title.upcase] = id
end

BATCH_SIZE = 10000
total_page_count = mysql_client.query('SELECT count(*) FROM Page;').first['count(*)'].to_i
processed_page_count = 120000 # 3138700 # 1_162_700

while processed_page_count < total_page_count
  puts "Processing #{processed_page_count + BATCH_SIZE} of #{total_page_count}"
  pages = mysql_client.query("
      SELECT id, name, SUBSTRING_INDEX(text, \"\n==\", 1) as abstract
      FROM Page
      LIMIT #{processed_page_count}, #{BATCH_SIZE};")
  processed_page_count += BATCH_SIZE

  links = []
  pages.each do |page|
    page['abstract'].scan(/\[\[(.+?)\]\]/) do |link|
      # [[Neurodevelopmental disorder|disorder of neural development]]
      link = link.first.split('|').first
      next if link.nil?

      link = link.upcase
      next unless level1_title_map_id[link]

      # puts ' -- ' + page['name'] + ' > ' + link
      links << { page: page, level1_page_id: level1_title_map_id[link] }
    end
  end

  links.each_with_index do |link, index|
    ((index + 1) % 60) == 0 ? puts('.') : print('.')
    # Create incoming page node
    page_id = link[:page]['id']
    page_title = link[:page]['name']
    level1_page_id = link[:level1_page_id]

    neo4j_client.execute_query("
        MERGE (p:Page {id: #{page_id}, title: \"#{mysql_client.escape(page_title)}\"})")

    neo4j_client.execute_query("
        MATCH (p:Page {id: #{page_id}}), (l:Page {id: #{level1_page_id}})
        MERGE p -[:INCOMING]-> l")
  end

  puts ''
end

