'use strict';
// TODO: when title is category

var fs = require('fs');
var hotSearchesJson = fs.readFileSync('configs/hot_search.json').toString().replace(/\/\/.+\n/g, '');
var hotSearches = JSON.parse(hotSearchesJson);

var wikiPages = [];
var processedWikiTitles = {};


function createTask(i){
  return function(notifyEndTaskCallback){
    console.log('Start task for index ', i);

    var searchDate = parseInt(hotSearches[i].date);
    var keywords = hotSearches[i].keywords;
    var keywordCount = keywords.length;
    var processedKeywordCount = 0;

    console.log('Searching for wiki titles for the keywords.');

    var async = require('async');
    var graph = require('../lib/graph');
    var neo4j = require('neo4j-js');
    neo4j.connect('http://localhost:7474/db/data/', function (err, neo4j) {
      async.map(keywords, graph.searchWikiTitles.bind(graph), function(err, results){
        for (var i = 0; i < results.length; i++) {
          if(!results[i]){ // null means error occurs while doing google search.
            keywordCount--;
            continue;
          }

          (function(keyword, titles, searchDate){
            // create keyword node
            neo4j.query('MERGE (:Keyword {name: {name}, date: {date}})',
                {name: keyword, date: searchDate}, function(){});

            // convert title
            var sqlTitles = [];
            for (var j = 0; j < titles.length; j++) {
              sqlTitles.push(titles[j].title);
            }

            var mysql = require('mysql');
            var sql = mysql.createConnection({
              'host': 'localhost',
              'port': 3306,
              'user': 'root',
              'password': '',
              'database': 'wiki-langs'
            });

            sql.connect();
            // search for wiki pages in sql
            var sqlStr = 'SELECT page_id as id, page_title as title FROM page '+
              'WHERE page_namespace = 0 and page_title IN ('+mysql.escape(sqlTitles)+')';
            sql.query({
              sql: sqlStr,
              typeCast: function (field, next) {
                if (field.type === 'VAR_STRING') {
                  return field.string();
                }
                return next();
              }
            }, function(err, rows){
              if(err) console.log(err);

              for (var i = 0; i < rows.length; i++) {
                var page = {title: rows[i].title, level: 1, id: rows[i].id, keyword: keyword, keywordDate: searchDate};
                wikiPages.push(page);

                processedWikiTitles[page.title] = true;

                // create page node and link keyword and page
                neo4j.query('MERGE (:Page {title: {title}, level: {level}, id: {id}})',
                  page, function(err){if(err) console.log(err);}
                );
                neo4j.query('MATCH (k:Keyword {name: {keyword}, date:{keywordDate}}), '+
                  '(p:Page {title: {title}, level: {level}, id: {id}}) MERGE k -[:MATCHES]-> p',
                  page, function(err){if(err) console.log(err);}
                );
              }

              // notify done
              processedKeywordCount += 1;
              if(processedKeywordCount === keywordCount){
                notifyEndTaskCallback(null, true);
                console.log("End of task for index ", i);
              }
            });
            sql.end();
          })(keywords[i], results[i], searchDate);
        }
      });
    });
  };
}

var taskFuncs = [];
for(var i = 0; i < hotSearches.length; i++){
  taskFuncs.push(createTask(i));
}

var async = require('async');
async.series(taskFuncs, function(){
  console.log('All task done, building page graphs');
  buildPagesGraph();
});


var MAX_LEVEL = 2;
var MAX_PAGES = 50;
function buildPagesGraph(){
  var selectedLevel;
  var page;
  var selectedPages = [];
  while((page = wikiPages.shift()) !== undefined){
    // console.log("page [" + page.title + "], level: " + page.level);
    if(page.level > MAX_LEVEL) continue;

    if(selectedPages.length > MAX_PAGES) break;

    if(!selectedLevel) {
      selectedLevel = page.level;
      selectedPages.push(page);
    }else{
      if(page.level === selectedLevel){
        selectedPages.push(page);
      }else{
        wikiPages.unshift(page);
        break;
      }
    }
  }

  console.log('====== ' + wikiPages.length + ' pages, level ' + selectedLevel);

  if(selectedPages.length === 0) return; // all pages has been processed

  var pageIds = [];
  for (var i = 0; i < selectedPages.length; i++) {
    pageIds.push(selectedPages[i].id);
  }

  // console.log("processing level [" + selectedLevel + "] of ids [" + pageIds.join(", ") + "]");

  var wiki = require('../lib/wiki');
  wiki.getPageInternalLinks(pageIds, function(results){
    var nextLevelTitles = [];
    var titleMapParentPageId = {};
    for(var i = 0; i < results.length; i++){
      var pageId = results[i].pageId;
      var links = results[i].links;
      for (var j = 0; j < links.length; j++) {
        var uTitle = links[j].replace(/ /g, '_');
        nextLevelTitles.push(uTitle);
        titleMapParentPageId[uTitle] = pageId;
      }
    }
    // console.log("nextLevelTitles: " + nextLevelTitles);

    var mysql = require('mysql');
    var sql = mysql.createConnection({
      'host': 'localhost',
      'port': 3306,
      'user': 'root',
      'password': '',
      'database': 'wiki-langs'
    });

    sql.connect();
    var sqlStr = 'SELECT page_id as id, page_title as title '+
      'FROM page WHERE page_namespace = 0 and page_title IN ('+mysql.escape(nextLevelTitles)+')';
    sql.query({
      sql: sqlStr,
      typeCast: function (field, next) {
        if (field.type === 'VAR_STRING') {
          return field.string();
        }
        return next();
      }
    }, function(err, rows){
      if(err) console.log(err);
      var neo4j = require('neo4j-js');
      neo4j.connect('http://localhost:7474/db/data/', function (err, neo4j) {
        if(err) console.log(err);

        // var graphBatch = neo4j.createBatch();
        for (var i = 0; i < rows.length; i++) {
          // ignore created or parent node
          if(processedWikiTitles[rows[i].title]) continue;

          var page = {id: rows[i].id, title: rows[i].title, level: selectedLevel+1};
          if(page.level > MAX_LEVEL) continue;

          processedWikiTitles[page.title] = true;

          // build nodes
          neo4j.query('MERGE (:Page {id: {id}, title: {title}, level: {level}})', page, function(err){
            if(err) console.log(err);
          });

          // add relations
          var parentPageId = titleMapParentPageId[page.title];
          if(parentPageId){
            // console.log("parentsPageId: "+ parentPageId + " child " + page.id + " " + page.title);
            neo4j.query('MATCH (p:Page {id: {parentId}}), (cp:Page {id: {childId}}) MERGE cp -[:LEVEL_'+
              (selectedLevel+1)+']-> p',
              {parentId: parseInt(parentPageId), childId: page.id}, function(err){if(err) console.log(err);}
            );
          }

          wikiPages.push(page);
        }
        // graphBatch.run();
        buildPagesGraph();
      });
    });
    sql.end();
  });
}

