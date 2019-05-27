
export interface Timing {
  metric: string;
  time: number;
}

export interface FilmstripViewThumbnail {
  time: number;
  imageUrl: string;
  hasChange: boolean;
  isHighlighted: boolean;
  isOffset: boolean;
  timings: Timing[];
}

export declare type FilmstripView = FilmstripViewThumbnail[];
