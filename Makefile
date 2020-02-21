.PHONY: prepare test coverage lint clean

node_modules/ws:
	npm install
prepare: node_modules/ws

test: prepare
	lein test-all

coverage:
	lein cloverage --codecov

lint:
	clj-kondo --lint src:test
	cljstyle check

clean:
	lein clean
	\rm -rf node_modules
