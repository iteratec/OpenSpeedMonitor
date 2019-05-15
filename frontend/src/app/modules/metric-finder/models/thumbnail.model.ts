export interface ThumbnailDto {
  time: number;
  image: string;
}

export class Thumbnail {
  public time: number;
  public imageUrl: string;

  constructor(time: number, imageUrl: string) {
    this.time = time;
    this.imageUrl = imageUrl;
  }
}
