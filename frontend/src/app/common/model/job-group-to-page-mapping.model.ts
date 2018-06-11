import {IPageId} from "./page.model";

export interface IJobGroupToPagesMapping {
  id: number;
  name: string;
  pages: IPageId[];
}
