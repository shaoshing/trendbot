
const DB_CONFIG_PATH = "configs/database.json"

var fs = require('fs');
var mysqlConfig = JSON.parse(fs.readFileSync(DB_CONFIG_PATH));

var mysql = require('mysql');
var connection = mysql.createConnection(mysqlConfig);

connection.connect();

connection.query('SELECT * FROM pages LIMIT 1', function(err, rows, fields) {
  if (err) throw err;

  console.log('The solution is: ', rows[0]);
});

connection.end();
