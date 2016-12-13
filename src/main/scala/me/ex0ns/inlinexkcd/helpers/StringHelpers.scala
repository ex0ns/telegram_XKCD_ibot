package me.ex0ns.inlinexkcd.helpers

/**
  * Created by thibault on 12/12/16.
  */

object StringHelpers {
	implicit class MarkdownString(string: String) {
		def bold = s"*$string*"
		def italic = s"_${string}_"
		def urlWithAlt(alt: String) = s"[$alt]($string)"
		def altWithUrl(url: String) = s"[$string]($url)"
		def inlineCode = s"`$string`"
		def blockCode = s"```$string```"
	}
}
