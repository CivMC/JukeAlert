plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "JukeAlert"
	}
}

dependencies {
	paperDevBundle("1.19.4-R0.1-SNAPSHOT")

	compileOnly("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
	compileOnly("net.civmc:namelayer-spigot:3.0.0-SNAPSHOT:dev")
	compileOnly("net.civmc:citadel:5.0.0-SNAPSHOT:dev")
}
