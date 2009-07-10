require 'rubygems'
require 'json'
require 'enunciate.rb'

classname = ARGV[0]
infile = ARGV[1]
outfile = ARGV[2]

filecontents = ''
File.open(infile, 'r') do |thefile|
  filecontents = thefile.read
end

parsed = JSON.parse(filecontents)
o = eval "#{classname}.from_json(parsed)"
File.open(outfile, 'w') do |out|
  out.write(o.to_json)
end
