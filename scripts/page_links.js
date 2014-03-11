

var neo4j = require("neo4j-js");
neo4j.connect("http://localhost:7474/db/data/", function(err, graph){
  if(err) throw err;

  console.log("Running Cypher");
  graph.query('MATCH tweet --> (:Hashtag:Twitter) --> (p:Page) <-- (:Hashtag:Weibo) <-- weibo '+
    'RETURN tweet.tweetId as tweetId, p.zh_title as zhTitle, p.en_title as enTitle, weibo.id as weiboId;',
    function(err, results){
      if(err) throw err;

      console.log("- Done");
      // Example
      // +---------------------------------------------------------------+
      // | mm.tweetId           | p.zh_title | p.en_title | m.id         |
      // +---------------------------------------------------------------+
      // | "161919961135198208" | "测试"       | "Test"     | "mexHWuM5YG" |
      // | "161886356124090369" | "测试"       | "Test"     | "mexHWuM5YG" |
      // | "163261433872777216" | "测试"       | "Test"     | "mexHWuM5YG" |
      // | "162164581257588736" | "测试"       | "Test"     | "mexHWuM5YG" |
      // | "162095413221015552" | "测试"       | "Test"     | "mexHWuM5YG" |
      // | "163097486909636608" | "测试"       | "Test"     | "mexHWuM5YG" |
      // | "163436071835811841" | "测试"       | "Test"     | "mexHWuM5YG" |
      console.log("Counting links");
      var processedId = {};
      var pages = {};
      for (var i = 0; i < results.length; i++) {
        var row = results[i];
        var title = row.enTitle + ", " + row.zhTitle;
        if(!pages[title]) pages[title] = {tweet: 0, weibo: 0};

        var tweetIdKey = title + row.tweetId;
        if(!processedId[tweetIdKey]){
          processedId[tweetIdKey] = true;
          pages[title].tweet += 1;
        }

        var weiboIdKey = title + row.weiboId;
        if(!processedId[weiboIdKey]){
          processedId[weiboIdKey] = true;
          pages[title].weibo += 1;
        }
      };

      console.log("Generating CSV");
      var csvTitle = "en_title, zh_title, tweet_links, weibo_links";
      var csvLines = []
      for (var title in pages) {
        var line = title;
        line += ", " + pages[title].tweet + ", " + pages[title].weibo;
        csvLines.push(line);
      };
      var array = require("array");
      csvLines = array(csvLines).sort();

      var csvContent = csvTitle + "\n" + csvLines.join("\n");
      var fs = require("fs");
      fs.writeFileSync("tmp/links.csv", csvContent);

      console.log("Done");
    }
  );
});
