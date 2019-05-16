import {ThumbnailDto} from './thumbnail.model';

export interface WptResultDTO {
  data: {
    runs: {
      [runNumber: number]: {
        firstView: {
          steps: {
            videoFrames: ThumbnailDto[]
          }[]
        }
      }
    }
  };
}
