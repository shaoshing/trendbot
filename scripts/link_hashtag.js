var sample = require("../lib/sample");

console.log("Loading weibo hashtags");
var weiboHashtags = [];
sample.readLines("weibo-hashtag.txt", function(line){
  weiboHashtags.push(line);
}, function(){ // done
  console.log("Finished loading weibo hashtags");
});
