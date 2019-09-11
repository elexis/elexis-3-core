# Rocketchat Logback Appender

This fragment connects logback to [Rocketchat](url "https://rocket.chat").

### Configuration Rocketchat

See [Integrations](url https://rocket.chat/docs/administrator-guides/integrations/) for information on how to create an incoming webhook.

### Configuration Appender

The following sample logback configuration includes this appender. It is not recommended to use the appender for log levels lower WARN. 

	<sample>
		<appender name="Rocketchat"
			class="ch.elexis.core.logback.rocketchat.RocketchatAppender">
			<identification>[TEST] DEVELOPMENT</identification>
			<integrationUrl>https://rocketchat.medelexis.ch/hooks/hookUrl</integrationUrl>
		</appender>
		<root level="WARN">
			<appender-ref ref="Rocketchat" />
		</root>
	</sample>
