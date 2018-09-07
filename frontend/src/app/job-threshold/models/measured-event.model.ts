/**
 * Created by glastra on 27.06.18.
 */
import {TestedPage} from "./tested-page.model";
export type MeasuredEvent = {
  id: number;
  name: string;
  testedPage: TestedPage;
  testedPageId: number;
  state: string;
}
