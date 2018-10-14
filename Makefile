TOP_LEVEL_V = top_level.v

CODE_FILES = $(shell find src/ -type f -name '*')

top_level.bit: *.tcl ${TOP_LEVEL_V} *.xdc
	vivado -mode batch -source build.tcl -verbose -nojournal

${TOP_LEVEL_V}: ${CODE_FILES}
	sbt "runMain hdmi.TopLevelVerilog"

.PHONY: sim deploy clean

deploy: top_level.bit
	vivado -mode batch -source deploy.tcl -verbose -nojournal

clean:
	rm -rf target tmp simWorkspace ${TOP_LEVEL_V} *.log *.jou *.bit usage_statistics_webtalk.*
