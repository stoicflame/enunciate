#!/bin/sh

PRG="$0"
TEST_HOME=`dirname $PRG`

if [ ! -e $TEST_HOME/target/enunciate/compile/csharp/full.dll ]
then
  echo "$TEST_HOME/target/enunciate/compile/csharp/full.dll doesn't exist"
  exit 1
fi

mkdir $TEST_HOME/target/cstests > /dev/null
cp $TEST_HOME/target/enunciate/compile/csharp/full.dll $TEST_HOME/target/cstests
gmcs $TEST_HOME/src/test/cs/TestCSharpClient.cs /out:$TEST_HOME/target/cstests/fulltests.dll /target:library /r:nunit.framework /r:System.Web.Services /r:$TEST_HOME/target/cstests/full.dll
if [ 0 != $? ]
then
  echo "C# compile failed"
  exit 1
fi
bash -c cd $TEST_HOME/target && nunit-console $TEST_HOME/target/cstests/fulltests.dll