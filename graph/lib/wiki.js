// Example:
// var Wiki = require("./lib/wiki");
// Wiki.getPageInternalLinks(["Anarchism", "political philosophy"], function(data){
//   console.log("Done");
//   pages = data;
// });


(function(){
  'use strict';
  var Wiki = {};

  Wiki.getPageInternalLinks = function(pageTitlesOrIds, callback){
    var Bot = require('nodemw');

    var client = new Bot({
        server: 'en.wikipedia.org',
        path: '/w',
        debug: false
      });

    client.getArticles(pageTitlesOrIds.join('|'), function(data) {
      // data:
      // {
      // '12':
      //  { pageid: 12,
      //    ns: 0,
      //    title: 'Anarchism',
      //    revisions: [ [Object] ] },
      // '23040':
      //  { pageid: 23040,
      //    ns: 0,
      //    title: 'Political philosophy
      //    revisions: [ [Object] ] }
      // }
      var pages = [];
      for (var pageId in data) {
        var pageData = data[pageId];
        var pageContent = pageData.revisions && pageData.revisions[0] && pageData.revisions[0]['*'];
        if(!pageContent)
          continue;

        var page = {
          pageId: pageId,
          title: pageData.title,
          links: []
        };
        var pageAbstract = pageContent.split('\n==')[0];
        var linkReg = /\[\[(.+?)\]\]/g;
        var match;
        while((match = linkReg.exec(pageAbstract)) !== null){
          // deal with alias internal link, for example: [[actual name|alias name]]
          var link = match[1].split('|')[0];
          page.links.push(link);
        }

        pages.push(page);
      }

      callback(pages);
    });
  };

  module.exports = Wiki;
})();
