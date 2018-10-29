import {PageDto} from "./page.model";

export interface JobGroupToPagesMappingDto {
  id: number;
  name: string;
  pages: PageDto[];
}
