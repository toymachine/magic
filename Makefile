clean:
	find . -name '*.jar' -exec rm -rf {} \;

deploy: deps
	lein clean
	lein appengine-prepare
	$(GAE_JAVA_SDK_HOME)/bin/appcfg.sh update war

rundev: deps
	lein clean
	lein appengine-prepare
	$(GAE_JAVA_SDK_HOME)/bin/dev_appserver.sh war

deps: clean
	lein deps
