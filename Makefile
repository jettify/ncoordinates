# Some simple testing tasks (sorry, UNIX only).

FLAGS=


lint:
	sbt scapegoat scalastyle


cov cover coverage:
	sbt coverage test coverageReport


clean:
	sbt clean


doc:
	make -C docs html
	@echo "open file://`pwd`/docs/_build/html/index.html"

.PHONY: all flake test vtest cov clean doc ci
