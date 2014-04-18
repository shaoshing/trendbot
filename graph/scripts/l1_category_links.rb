# Encoding: utf-8

require 'rubygems'
require 'neography'
require 'json'
require 'mysql2'

mysql_client = Mysql2::Client.new(
  host: 'localhost',
  username: 'root',
  database: 'wiki-langs'
)

neo4j_client = Neography::Rest.new
File.read('graph/data/level2_categories.txt').lines.each do |line|
  l2_id, l2_title, l1_id, l1_title = line.strip.split(', ', 4)
  puts "#{l2_title} -> #{l1_title}"

  neo4j_client.execute_query("
      MERGE (c:Category {
        id: #{l1_id},
        title: \"#{mysql_client.escape(l1_title)}\",
        level: 1
      })")

  neo4j_client.execute_query("
      MATCH (c1:Category {id: #{l1_id}, level: 1}), (c2:Category {id: #{l2_id}, level: 2}) <-[:L1_BELONGS_TO]- p
      MERGE c1 <-[:BELONGS_TO_CATEGORY]- c2
      MERGE c1 <-[:L1_BELONGS_TO]- p")
end
