Basic streaming pipeline with basketball match data.

Data consists of series of `ScoreEvent` which reflects changes in match score,  and  it's streamed during the match.

Data is initially encoded as hex strings and could contain invalid data.

Events could arrive out of order, we just mark them as invalid in that case.

Stream doesn't fail on managed encoding and validation exceptions but this information  is conserved  in a form of consumable error stream channel.

Stream is stateful and in this basic implementation we accumulate all events in-memory ( we have a limited number of match events)
- to be able to do simple validations  ( comparing  new event ot prev. one )
- to provide simple streaming interface which allows to stream last N events out of system



