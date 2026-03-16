package io.cequence.wsclient.service

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.libs.json.JsValue

/**
 * Stream request support specifically tailored for OpenAI API.
 *
 * @since Feb
 *   2023
 */
trait WSClientOutputStreamExtra {

  protected implicit val materializer: Materializer

  def execJsonStream(
    endPoint: String,
    method: String,
    endPointParam: Option[String] = None,
    params: Seq[(String, Option[Any])] = Nil,
    bodyParams: Seq[(String, Option[JsValue])] = Nil,
    extraHeaders: Seq[(String, String)] = Nil,
    framingDelimiter: String = "\n\n",
    maxFrameLength: Option[Int] = None,
    stripPrefix: Option[String] = None,
    stripSuffix: Option[String] = None
  ): Source[JsValue, NotUsed]

  def execRawStream(
    endPoint: String,
    method: String,
    endPointParam: Option[String],
    params: Seq[(String, Option[Any])],
    bodyParams: Seq[(String, Option[JsValue])],
    extraHeaders: Seq[(String, String)]
  ): Source[ByteString, NotUsed]
}
