package gerritforge
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object EncodeUtils {
  val encoding = StandardCharsets.UTF_8.toString()

  def encode(value: String): String = {
    URLEncoder.encode(value, encoding)
  }
}
