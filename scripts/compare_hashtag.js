var sample = require("../lib/sample");


console.log("Loading weibo hashtags");
var weiboHashtags = {};
sample.readLines("weibo-hashtag.txt", function(line){
  weiboHashtags[line] = true;
});

console.log("Comparing with Twitter hashtags");
var fs = require("fs");
sample.readLines("twitter-hashtag.txt", function(line){
  if(weiboHashtags[line]){
    console.log("matched: " + line);
    fs.appendFileSync("tmp/hashtags.txt", line+"\n");
  }
});

