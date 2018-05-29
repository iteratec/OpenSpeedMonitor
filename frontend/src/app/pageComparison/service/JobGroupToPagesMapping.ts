import {PageId} from "./PageId";

export class JobGroupToPagesMapping{
    id:string;
    name:string;
    pages: PageId[];

    static createFromJSON(id:string, json:any): JobGroupToPagesMapping{
      let mapping:JobGroupToPagesMapping = new JobGroupToPagesMapping();
      mapping.id = id;
      mapping.name = json.name;
      mapping.pages = json.pages.map(pageJson => PageId.createFromJson(pageJson));
      return mapping
    }

}
