require 'rubygems'
require 'json'
require 'json/add/core'

module Org
  module FamilySearch
    module Ws
      class Person
        attr_accessor :name
        attr_accessor :gender
        attr_accessor :cool

        def to_json_hash
          h = {}
          h['name'] = name
          h['gender'] = gender
          h['cool'] = cool
          return h
        end

        def to_json
          to_json_hash.to_json
        end

        def from_json_hash(o)
          @name= o['name']
          @gender= o['gender']
          @cool= o['cool']
        end

        def self.from_json(o)
          inst = new
          inst.from_json_hash o
          return inst
        end
      end
    end
  end
end

module Org
  module FamilySearch
    module Ws
      class ExPerson < Org::FamilySearch::Ws::Person
        attr_accessor :birth
        attr_accessor :name

        def to_json_hash
          h = super
          h['birth'] = birth
          return h
        end

        def from_json_hash(o)
          super o
          @birth = o['birth']
        end

        def self.from_json(o)
          inst = new
          inst.from_json_hash o
          return inst
        end
      end
    end
  end
end

mine = Org::FamilySearch::Ws::Person.new()
mine.name = 'my name'
mine.gender = 'male'
mine.cool = true

puts
puts "mine: " + mine.inspect
json = mine.to_json
puts "json: " + json

parsed  = JSON.parse(json)
puts "parsed: " + parsed.inspect
another = Org::FamilySearch::Ws::Person.from_json(parsed)
puts "another: " + another.inspect
puts "another json: " + another.to_json
puts

mine = Org::FamilySearch::Ws::ExPerson.new()
mine.name = 'my name'
mine.gender = 'male'
mine.cool = true
mine.birth = 'a long time ago'

puts "mine: " + mine.inspect
json = mine.to_json
puts "json: " + json

parsed = JSON.parse(json)
puts "parsed: " + parsed.inspect
another = Org::FamilySearch::Ws::ExPerson.from_json(parsed)
puts "another: " + another.inspect
puts "another json: " + another.to_json
puts