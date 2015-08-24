require 'rubygems'
require 'json'
require 'json/add/core'

# adding necessary json serialization methods to standard classes.
class Object
  def to_jaxb_json_hash
    return self
  end
  def self.from_json o
    return o
  end
end

class String
  def self.from_json o
    return o
  end
end

class Boolean
  def self.from_json o
    return o
  end
end

class Numeric
  def self.from_json o
    return o
  end
end

class Time
  #json time is represented as number of milliseconds since epoch
  def to_jaxb_json_hash
    return (to_i * 1000) + (usec / 1000)
  end
  def self.from_json o
    return Time.at(o / 1000, (o % 1000) * 1000)
  end
end

class Array
  def to_jaxb_json_hash
    a = Array.new
    each { | _item | a.push _item.to_jaxb_json_hash }
    return a
  end
end

module Org
  module FamilySearch
    module Ws
      class Person
        attr_accessor :name
        attr_accessor :gender
        attr_accessor :cool

        def initialize
        end

        def to_jaxb_json_hash
          h = {}
          h['name'] = name.to_jaxb_json_hash
          h['gender'] = gender.to_jaxb_json_hash
          h['cool'] = cool.to_jaxb_json_hash
          return h
        end

        def to_json
          to_jaxb_json_hash.to_json
        end

        def init_jaxb_json_hash(o)
          @name= String.from_json o['name']
          @gender= String.from_json o['gender']
          @cool= Boolean.from_json o['cool']
          @sweet = Org::FamilySearch::Ws::ExPerson.from_json o['sweet']
        end

        def self.from_json(o)
          inst = new
          inst.init_jaxb_json_hash o
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
        attr_accessor :parents

        def initialize
          super
          @parents = Array.new
        end

        def to_jaxb_json_hash
          h = super
          h['birth'] = birth.to_jaxb_json_hash
          ha = Array.new
          parents.each { | _parent | ha.push _parent.to_jaxb_json_hash }
          h['parents'] = ha
          return h
        end

        def init_jaxb_json_hash(o)
          super o
          @birth = o['birth']
          @parents = Array.new
          _oa = o['parents']
          _oa.each { | parent | @parents.push Org::FamilySearch::Ws::Person.from_json(parent) }
        end

        def self.from_json(o)
          if o.nil?
            return nil
          end
          
          inst = new
          inst.init_jaxb_json_hash o
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
puts "ex new " + mine.inspect
mine.name = 'my name'
mine.gender = 'male'
mine.cool = true
mine.birth = 'a long time ago'
mine.parents = Array.new
mine.parents.push(Org::FamilySearch::Ws::Person.new())
mine.parents[0].name = 'parent one name'
mine.parents[0].gender = 'male'
mine.parents[0].cool = false
mine.parents.push(Org::FamilySearch::Ws::Person.new())
mine.parents[1].name = 'parent two name'
mine.parents[1].gender = 'female'
mine.parents[1].cool = true

puts "mine: " + mine.inspect
json = mine.to_json
puts "json: " + json

parsed = JSON.parse(json)
puts "parsed: " + parsed.inspect
another = Org::FamilySearch::Ws::ExPerson.from_json(parsed)
puts "another: " + another.inspect
puts "another json: " + another.to_json
puts
puts

another = eval "Org::FamilySearch::Ws::ExPerson.from_json(parsed)"
puts "another: " + another.inspect
puts "another json: " + another.to_json