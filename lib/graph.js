

(function(){
  var Graph = {};
  module.exports = Graph;


  Graph.buildGraphFromGoogleSearch = function(keywords, depth, callback){
    var db = this.getDB();

    var existingNodeIds = {};
    var wikiPageQueue = [];

    // Search for pages
    // Load cache from tmp/
    for (var i = 0; i < keywords.length; i++) {
      keywords[i]
    };

    // get a list of visited pages/categories from page view

    // page node: title, id, depth
    // category node: title, id, depth

    // build relationship between pages

    // build relationship between pages and categories


    // build relationship between categories (main, subcategories)

  }

  const NEO4J_CONFIG = "configs/neo4j.json";
  Graph.getDB = function(){
    if(!this._db){
      var async = require("async");
      var fs = require("fs");
      var neo4j = require("neo4j");

      try{
        var neo4jConfigs = JSON.parse(fs.readFileSync(NEO4J_CONFIG));
      }catch(err){
        console.log("Please copy a example config from configs to " + NEO4J_CONFIG);
        throw err;
      }
      this._db = new neo4j.GraphDatabase(neo4jConfigs.host);
    }

    return this._db;
  }

  const GOOGLE_SEARCH_CACHE = "/tmp/google_search_cache.json";
  // Google Custom Search API: https://developers.google.com/custom-search/json-api/v1/reference/cse/list
  const GOOGLE_SEARCH_URL = "https://www.googleapis.com/customsearch/v1?num=10&"+
    "key=AIzaSyA96mFm7cicTMmPCJydpXe_S14iqwopUFk&cx=014520003406286081382%3Aeywh_ejzy3e&q=";
  const MAX_WIKI_PAGES_FROM_SEARCH = 5;
  Graph.searchWikiTitles = function(keyword, callback){
    // Read cache
    if(!this._googleSearchCache){
      this._googleSearchCache = {};
      try{
        this._googleSearchCache = JSON.parse(fs.readFileSync(GOOGLE_SEARCH_CACHE));
      }catch(err){}
    }

    if(this._googleSearchCache[keyword])
      return this._googleSearchCache[keyword];

    // Make request
    var https = require("https");
    var url = GOOGLE_SEARCH_URL + escape(keyword);
    var wikiPageTitles = [];
    var googleSearchCache = this._googleSearchCache;
    console.log(url);
    https.get(url, function(res) {
      console.log("Got response: " + res.statusCode);

      // Body example: http://cl.ly/code/2y060h0L2v19
      var body = "";
      res.on("data", function(data) {
        body += data;
      });

      res.on('end',function(){
        var result = JSON.parse(body);
        var item;
        while(item = result.items.shift()){
          // Title example:
          //    Anarchism - Wikipedia, the free encyclopedia
          //    Portal:Anarchism - Wikipedia, the free encyclopedia
          //    Category:Anarchism - Wikipedia, the free encyclopedia
          console.log(item.title);

          var title = item.title.split(" - ")[0];

          // Ignore these titles:
          //    Portal:Anarchism
          //    Category:Anarchism
          if(title.match(":")) continue;

          wikiPageTitles.push(title);
          if(wikiPageTitles.length == MAX_WIKI_PAGES_FROM_SEARCH) break;
        }

        console.log("wikiPageTitles");
        console.log(wikiPageTitles);
        googleSearchCache[keyword] = wikiPageTitles;
        fs.writeFileSync(GOOGLE_SEARCH_CACHE, JSON.stringify(googleSearchCache));

        callback(wikiPageTitles);
      });
    }).on('error', function(e) {
      console.log("Got error: " + e.message);
      throw e;
    });
  };
})();
