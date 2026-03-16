package io.cequence.wsclient.service.adapter

import org.apache.pekko.stream.Materializer
import io.cequence.wsclient.service.CloseableService

trait ServiceBaseAdaptersAkka[S <: CloseableService] { self: ServiceBaseAdapters[S] =>

  def parallelTakeFirst(
    underlyings: S*
  )(
    implicit materializer: Materializer
  ): S =
    wrapAndDelegate(new ParallelTakeFirstAdapter(underlyings))
}
