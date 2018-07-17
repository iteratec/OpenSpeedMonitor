import {PageIdDto} from "./page.model";

export type JobGroupToPagesMappingDto = {
  id: number;
  name: string;
  pages: PageIdDto[];
}
