package io.cequence.wsclient.domain

import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString

trait StreamedResponse extends Response {
  def source: Source[ByteString, _]
}
