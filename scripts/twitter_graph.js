

// Connect database
var mysql = require('mysql');
sql = mysql.createConnection({
  "host": "localhost",
  "port": 3306,
  "user": "root",
  "password": "",
  "database": "wiki"
});

var data = [
// {
//   file: "weibo-hashtag.txt",
//   language: 1,
//   zh_title: "title",
//   en_title: "translation",
//   label: "Weibo",
//   match: "zh_title"
// }
// ,
{
  file: "twitter-hashtag.txt",
  language: 0,
  zh_title: "translation",
  en_title: "title",
  label: "Twitter",
  match: "en_title"
}
];

for (var i = 0; i < data.length; i++) {
  d = data[i];

  // load hashtag
  console.log("Loading hashtags for " + d.file);
  var hashtags = [];
  var sample = require("../lib/sample");
  sample.readLines(d.file, function(line){
    hashtags.push(line);
  }, function(){ // done loading
    const BATCH_SIZE = 100;
    sql.connect();
    var batchCount = Math.round(hashtags.length/BATCH_SIZE) + (hashtags.length % BATCH_SIZE == 0 ? 0 : 1);

    var processedCount = 0;
    for (var i = 0; i < batchCount; i++) {
      var slicedHashtags = hashtags.splice(0, Math.min(BATCH_SIZE, hashtags.length));
      var escapedHashtags = [];
      for (var j = 0; j < slicedHashtags.length; j++) {
        escapedHashtags.push(mysql.escape(slicedHashtags[j]));
      };

      //  query sql for wikipage
      var query = 'SELECT pages.title as title, pages.translation as translation '+
      'FROM redirections JOIN pages ON pages.id = redirections.redirect_id AND pages.language = redirections.language'+
      ' WHERE redirections.language = '+d.language+' AND redirections.title IN ('+ escapedHashtags.join(", ") +')';
      sql.query(query, function(err, rows) {
        if (err){
          console.log(err.message);
          return;
        }
        console.log("Found " + rows.length + " pages");

        var neo4j = require('neo4j-js');
        neo4j.connect('http://localhost:7474/db/data/', function (err, graph) {
          if (err) throw err;

          var graphBatch = graph.createBatch();
          for (var i = 0; i < rows.length; i++) {
            var row = rows[i]
            var pageNodeAttrs = {zh_title: row[d.zh_title], en_title: row[d.en_title]};
            pageNodeAttrs.hashtagReg = "(?i)" + pageNodeAttrs[d.match];

            //  create wiki node for matched page
            graph.query(graphBatch,
              'MERGE (:Wiki:Page {zh_title: {zh_title}, en_title: {en_title}});',
              pageNodeAttrs,
              function(){});

            //  build connection of existing pages rows[i]
            graph.query(graphBatch,
              'MATCH (p:Wiki {'+d.match+': {'+d.match+'}}), (h:Hashtag:'+d.label+') WHERE h.name =~ {hashtagReg} MERGE h -[:MATCHES]-> p;',
              pageNodeAttrs,
              function(){});
          };

          console.log("Running cyphers");
          graphBatch.run();
          processedCount += BATCH_SIZE;
          console.log("Processed " + processedCount + " hashtags");
        });
      });
    };
    sql.end();
  });
};

