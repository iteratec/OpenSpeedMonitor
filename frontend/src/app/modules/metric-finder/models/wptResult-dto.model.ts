import {ThumbnailDto} from './thumbnail.model';

export interface WptResultDto {
  data: { runs: { firstView: { steps: { videoFrames: ThumbnailDto[] } } }[] };
}
