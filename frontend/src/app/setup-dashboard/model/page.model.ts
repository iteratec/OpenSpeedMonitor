export class Page {
  protected id: number;
  protected name: string;
  protected undefinedPage: boolean;

  public getId() {
    return this.id;
  }

  public getName() {
    return this.name;
  }

  public getUndefinedPage(){
    return this.undefinedPage;
  }
}

export class PageFromJson extends Page {
  constructor(JsonObject: any) {
    super();
    Object.assign(this, JsonObject);
  }
}

export class PageFromParams extends Page {
  constructor(id: number, name: string, undefinedPage: boolean) {
    super();
    this.id = id;
    this.name = name;
    this.undefinedPage = undefinedPage
  }
}
