// Usage: var db = require("./lib/db")

(function () {
  'use strict';
  var Database = {};

  var DB_CONFIG_PATH = 'configs/database.json';
  Database.getConfig = function(){
    if(!this._config){
      var fs = require('fs');
      this._config = JSON.parse(fs.readFileSync(DB_CONFIG_PATH));
    }
    return this._config;
  };

  // var connection = Database.getConnection();
  // connection.connect();
  // connection.query('SELECT * FROM pages LIMIT 1', function(err, rows, fields) {
  //   if (err) throw err;
  //   console.log('The solution is: ', rows[0]);
  // });a fasd f
  // connection.end();
  Database.getConnection = function(){
    if(!this._connection){
      var mysql = require('mysql');
      this._connectioasdn = mysql.createConnection(this.getConfig());
    }
    return this._cond;
  };

  module.exports = Database;
})();
