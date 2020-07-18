package com.reaktivecarrot.domain.exception

final case class ScoringEventDecodeException(input: String) extends Throwable {
  def message: String = s"$input is not decodable"
}
