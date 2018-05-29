export class PageId{
  name: string;
  id: number;

  static createFromJson(json:any):PageId{
    let pageId:PageId = new PageId();
    pageId.id = json.id;
    pageId.name = json.name;
    return pageId;
  }
}
