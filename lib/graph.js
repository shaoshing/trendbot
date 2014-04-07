(function(){
  'use strict';
  var Graph = {};
  module.exports = Graph;

  // Graph.buildGraphFromGoogleSearch = function(keywords, depth, callback){
  //   var db = this.getDB();

  //   var existingNodeIds = {};
  //   var wikiPageQueue = [];

  //   // Search for pages

  //   // get a list of visited pages/categories from page view

  //   // page node: title, id, depth
  //   // category node: title, id, depth

  //   // build relationship between pages

  //   // build relationship between pages and categories


  //   // build relationship between categories (main, subcategories)

  // };

  var NEO4J_CONFIG = 'configs/neo4j.json';
  Graph.getDB = function(){
    if(!this._db){
      var fs = require('fs');
      var neo4j = require('neo4j');
      var neo4jConfigs;
      try{
        neo4jConfigs = JSON.parse(fs.readFileSync(NEO4J_CONFIG));
      }catch(err){
        console.log('Please copy a example config from configs to ' + NEO4J_CONFIG);
        throw err;
      }
      this._db = new neo4j.GraphDatabase(neo4jConfigs.host);
    }

    return this._db;
  };

  // Google Custom Search API: https://developers.google.com/custom-search/json-api/v1/reference/cse/list
  var GOOGLE_SEARCH_URL = 'https://www.googleapis.com/customsearch/v1?num=10&'+
    'key=AIzaSyA96mFm7cicTMmPCJydpXe_S14iqwopUFk&cx=014520003406286081382%3Aeywh_ejzy3e&q=';
  Graph.searchWikiTitles = function(keyword, callback){

    if(this._readWikiTitlesCache(keyword)){
      callback(null, this._readWikiTitlesCache(keyword));
      return;
    }


    var url = GOOGLE_SEARCH_URL + escape(keyword);
    console.log('Graph.searchWikiTitles: making request to ' + url);

    var request = require('request');
    request(url, function (error, response, body) {
      if (error || response.statusCode !== 200) {
        console.log('Graph.searchWikiTitles: request error');
        callback(error, null);
        return;
      }

      var wikiPageTitles = Graph._extractWikiTitlesFromSearchResult(body);
      Graph._writeWikiTitlesCache(keyword, wikiPageTitles);

      console.log('Graph.searchWikiTitles: wiki titles for [' + keyword + ']:');
      console.log(wikiPageTitles.join(', '));
      callback(null, wikiPageTitles);
    });
  };

  var GOOGLE_SEARCH_CACHE = './tmp/google_search_cache.json';
  Graph._readWikiTitlesCache = function(keyword){
    // Read cache
    if(!this._googleSearchCache){
      this._googleSearchCache = {};
      try{
        var fs = require('fs');
        this._googleSearchCache = JSON.parse(fs.readFileSync(GOOGLE_SEARCH_CACHE));
      }catch(err){} // when cache file doesn't exist;
    }

    return this._googleSearchCache[keyword];
  };

  Graph._writeWikiTitlesCache = function(keyword, titles){
    // Making sure that the _googleSearchCache exist.
    this._readWikiTitlesCache(keyword);

    this._googleSearchCache[keyword] = titles;
    var fs = require('fs');
    fs.writeFileSync(GOOGLE_SEARCH_CACHE, JSON.stringify(this._googleSearchCache));
  };

  var MAX_WIKI_PAGES_FROM_SEARCH = 5;
  Graph._extractWikiTitlesFromSearchResult = function(resultBody){
    // Body example: http://cl.ly/code/2y060h0L2v19
    var result = JSON.parse(resultBody);
    var wikiPageLinks = [];
    var item;
    while((item = result.items.shift()) !== undefined){
      // Title example:
      //    Anarchism - Wikipedia, the free encyclopedia
      //    Portal:Anarchism - Wikipedia, the free encyclopedia
      //    Category:Anarchism - Wikipedia, the free encyclopedia
      var title = item.link.split('/wiki/')[1];
      if(!title) continue;

      var link = {title: title, isCategory: false};
      if(link.title.indexOf(':') !== -1){
        var typePrefix = title.split(':')[0].toLowerCase();
        if(typePrefix === 'category'){
          link.title = title.split(':')[1];
          link.isCategory = true;
        }else{
          // Neither a wiki page or a category page. e.g. Wikipedia
          continue;
        }
      }

      wikiPageLinks.push(link);
      if(wikiPageLinks.length === MAX_WIKI_PAGES_FROM_SEARCH) break;
    }
    return wikiPageLinks;
  };
})();
