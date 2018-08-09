import {PageCsiDto} from "./page-csi.model";

export interface PageCsiResponse {
  pageCsis: PageCsiDto[];
  isLoading: boolean;
}
