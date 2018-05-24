export class JobGroup {
  protected id: number;
  protected name: string;

  public getId() {
    return this.id;
  }

  public getName() {
    return this.name;
  }
}

export class JobGroupFromJson extends JobGroup {
  constructor(JsonObject: any) {
    super();
    Object.assign(this, JsonObject);
  }
}

export class JobGroupFromParams extends JobGroup {
  constructor(id: number, name: string) {
    super();
    this.id = id;
    this.name = name;
  }
}
