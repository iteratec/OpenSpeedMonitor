import {IPageId} from "./page.model";

export interface IJobGroupToPagesMapping {
  id: string;
  name: string;
  pages: IPageId[];
}
