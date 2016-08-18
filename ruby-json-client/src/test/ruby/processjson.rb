#
# Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

require 'rubygems'
require 'json'
require './api.rb'

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
