package me.ex0ns.inlinexkcd.helpers

object StringHelpers {

  implicit class MarkdownString(string: String) {
    def bold = s"*$string*"
    def italic = s"_${string}_"
    def urlWithAlt(alt: String) = s"[$alt](${safeMDUrl(string)})"
    def altWithUrl(url: String) = s"[$string](${safeMDUrl(url)})"
    def inlineCode = s"`$string`"
    def blockCode = s"```$string```"
  }

  private def safeMDUrl(url: String) : String =
    Seq("\\(" -> "%28", "\\)" -> "%29").foldLeft(url) { case (z, (s,r)) => z.replaceAll(s, r) }

}
