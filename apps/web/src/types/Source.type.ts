export interface Provenance {
    datasetTitle: string | null;
    publisher: string | null;
    datasetUrl: string | null;
    licenseId: string | null;
    fileName: string | null;
    fileUrl: string | null;
    locator: string | null;
    locatorLabel: string | null;
    retrievedAt: string | null;
    method: string;
    extractorVersion: string;
    confidence: number;
    synthetic: boolean;
}

export type FileStatus = "uvezeno" | "format_nije_podrzan" | "nestrukturirano" | "necitljivo" | "nedostupno";

export interface SourceFile {
    id: number;
    name: string | null;
    url: string;
    format: string | null;
    formatClass: "A" | "B" | "C" | "D" | null;
    status: FileStatus;
    httpStatus: number | null;
    retrievedAt: string;
    rows: number;
    contracts: number;
}

export interface SourceDataset {
    id: number;
    title: string | null;
    publisher: string | null;
    portal: string;
    landingUrl: string | null;
    licenseId: string | null;
    metadataModified: string | null;
    contracts: number;
    files: SourceFile[];
    synthetic: boolean;
}

export interface YearCount {
    year: number;
    contracts: number;
    incomplete: boolean;
}

export interface SourcesSummary {
    datasets: number;
    files: number;
    filesByStatus: Partial<Record<FileStatus, number>>;
    rows: number;
    contracts: number;
    buyersByType: Record<string, number>;
    contractsByYear: YearCount[];
    incompleteFromYear: number;
    hiddenContracts: number;
    issuesByCode: Record<string, number>;
    latestRetrievedAt: string | null;
}
