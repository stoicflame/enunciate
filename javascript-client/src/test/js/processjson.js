var fs = require('fs');
var api = require('./api.js');

// Capture the arguments
var classNamespaceParts = process.argv[1].split('::');
var infile = process.argv[2];
var outfile = process.argv[3];

// Load json from a file
var fileContents = fs.readFileSync(infile, 'utf8');

// Deserialize to an object
var parsed = JSON.parse(fileContents);
var classRef = getClassReference(api, classNamespaceParts);
var o = new classRef(parsed);

// Serialize object to json
fs.writeFileSync(outfile, o.toJSON());

/**
 * Given an array of class namespace parts ['Com', 'Webcohesion', 'Enunciate', 'Line'],
 * fetch and return a reference to the class
 */
function getClassReference(api, classNamespaceParts){
  var classRef = api;
  classNamespaceParts.forEach(function(part){
    classRef = classRef[part];
  });
  return classRef;
}