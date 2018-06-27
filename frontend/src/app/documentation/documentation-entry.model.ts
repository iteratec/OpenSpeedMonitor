import {DocumentationUrl} from "./documentation-url.model";

export type DocumentationEntry = {
  title: string;
  text: string;
  links: DocumentationUrl[];
  imgUrl: string;
}
