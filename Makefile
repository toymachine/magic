
deploy:
	lein clean
	lein appengine-prepare
	$(GAE_JAVA_SDK_HOME)/bin/appcfg.sh update war

rundev:
	lein clean
	lein appengine-prepare
	$(GAE_JAVA_SDK_HOME)/bin/dev_appserver.sh war

deps:
	find . -name '*.jar' -exec rm -rf {} \;
	lein deps
