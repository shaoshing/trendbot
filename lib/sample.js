(function(){
  var Sample = {};
  module.exports = Sample;


  const REPORT_ONE_EVERY_X_LINES = 1000000;

  // Example
  // var sample = require("./lib/sample"); sample.sampleFiles("/Users/shaoshing/Downloads/test/*.txt", "/Users/shaoshing/Downloads/out.txt", 0.5)
  //
  Sample.sampleFiles = function(inputPattern, output, percentageOrNumber, doneCallback){
    var glob = require("glob");

    // Truncate file
    var fs = require("fs");
    fs.writeFileSync(output, "");

    glob(inputPattern, function (err, files) {
      if(err){
        console.log(err);
        return;
      }

      Sample._countLines(files, function(countResult){
        var percent = percentageOrNumber;
        if(percent > 1){
          percent = percentageOrNumber / countResult.total;
        }
        console.log("Sample " + Math.round(percent*100) + "% of " + countResult.total + " lines");

        var finishedCount = 0;
        for (var i = 0; i < files.length; i++) {
          (function(){
            var fileName = files[i];
            var fileLineCount = countResult.files[fileName];
            var numberOfLinesToSelect = Math.round(percent * fileLineCount);
            if(numberOfLinesToSelect == 0){
              console.log(" - too few line (0) for " + fileName);
              return;
            }
            console.log(" - Sampling " + numberOfLinesToSelect + " lines from file: " + fileName);

            var lineNumbers = [];
            for (var j = 1; j <= fileLineCount; j++) {
              lineNumbers.push(j);
            };
            shuffleArray(lineNumbers);

            var selectedLines = {};
            for (var j = 0; j < numberOfLinesToSelect; j++) {
              selectedLines[lineNumbers[j]] = true;
            };

            var lineIndex = 0;
            Sample._readLines(fileName, function(line){
              if(selectedLines[lineIndex]){
                var fs = require("fs");
                fs.appendFileSync(output, line+"\n");
              }

              lineIndex += 1;
              if(lineIndex % REPORT_ONE_EVERY_X_LINES == 0){
                console.log(" - sampled " + lineIndex/1000000 + "M lines");
              }
            }, function(){ // end of file
              console.log(" - Finished sampling for " + fileName);

              finishedCount += 1;
              if(finishedCount == files.length){
                console.log(" - done");
                if(doneCallback) doneCallback();
              }
            });
          })();
        };
      });
    });
  };

  Sample._countLines = function(files, callback){
    console.log("Counting file lines");

    var result = {total: 0, files: {}};
    var finishedCount = 0;
    for (var i = 0; i < files.length; i++) {
      (function(){
        var fileName = files[i];
        result.files[files[i]] = 0;

        Sample._readLines(fileName, function(line){
          result.total += 1;
          result.files[fileName] += 1;

          if(result.total % REPORT_ONE_EVERY_X_LINES == 0){
            console.log(" - read " + result.total/1000000 + "M lines");
          }
        }, function(){ // on end
          finishedCount += 1;
          console.log(" - " + fileName + " " + result.files[fileName]);

          if(finishedCount == files.length){
            console.log(" - total: " + result.total);
            callback(result);
          }
        });
      })();
    };
  };

  Sample._readLines = function(file, callback, endCallback){
    var fs = require("fs");
    var remaining = '';
    fs.createReadStream(file).on('data', function(data) {
      remaining += data;
      var beginIndex = 0;
      for (var i = 0; i < remaining.length; i++) {
        // remaining.charCodeAt(i) is a lot faster than remainin[i]
        if(remaining.charCodeAt(i) == 10){ // linebreak
          var line = remaining.substring(beginIndex, i);
          beginIndex = i + 1;
          callback(line);
        }
      };
      remaining = remaining.substring(beginIndex, remaining.length);
    }).on('end', function(){
      endCallback();
    });
  };

  // http://stackoverflow.com/questions/6274339/how-can-i-shuffle-an-array-in-javascript
  function shuffleArray(o){ //v1.0
    for(var j, x, i = o.length; i; j = Math.floor(Math.random() * i), x = o[--i], o[i] = o[j], o[j] = x);
    return o;
  };
})();
