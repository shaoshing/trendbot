var async = require("async");
var fs = require("fs");
var neo4j = require("neo4j");

try{
  var neo4jConfigs = JSON.parse(fs.readFileSync("configs/neo4j.json"));
}catch(err){
  console.log("Please copy a example config from configs/neo4j.json.example to configs/neo4j.json")
  throw err;
}

var db = new neo4j.GraphDatabase(neo4jConfigs.host);


// get a list of visited pages/categories from page view

// page node: title, id
// category node: title, id

// build relationship between pages

// build relationship between pages and categories


// build relationship between categories (main, subcategories)
