#!/bin/sh

# OS specific support
DARWIN=false;
case "`uname`" in
  Darwin*)
    DARWIN=true
    ;;
esac

# Put some effort into finding ENUNCIATE_HOME (copies from ant's bash script)
if [ -z "$ENUNCIATE_HOME" -o ! -d "$ENUNCIATE_HOME" ] ; then
  # try to find enunciate...

  ## resolve links - $0 may be a link to enunciate's home
  PRG="$0"

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
  done

  ENUNCIATE_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  ENUNCIATE_HOME=`cd "$ENUNCIATE_HOME" && pwd`
fi

if [ -z "$ENUNCIATE_JAVA_HOME"  ] ; then
  ENUNCIATE_JAVA_HOME=$JAVA_HOME
  if [ -z "$ENUNCIATE_JAVA_HOME"  ] ; then
    echo "Error: ENUNCIATE_JAVA_HOME or JAVA_HOME is not defined correctly."
    exit 1
  fi
fi

if [ ! -x $ENUNCIATE_JAVA_HOME/bin/java ] ; then
  echo "Error: unable to execute $ENUNCIATE_JAVA_HOME/bin/java.  Is ENUNCIATE_JAVA_HOME or JAVA_HOME defined correctly?"
  exit 1
fi

if [ ! -f $ENUNCIATE_JAVA_HOME/lib/tools.jar ] ; then
  if [ "$DARWIN" != "true" ] ; then
    echo "Error: unable to find tools.jar in $ENUNCIATE_JAVA_HOME/lib/tools.jar.  Does ENUNCIATE_JAVA_HOME or JAVA_HOME point to a correct Java 5+ SDK home directory?"
    exit 1
  fi
fi

$ENUNCIATE_JAVA_HOME/bin/java -cp {UNIX_CLASSPATH}:$ENUNCIATE_HOME/{FULL_JAR_NAME}:$ENUNCIATE_JAVA_HOME/lib/tools.jar org.codehaus.enunciate.main.Main $@